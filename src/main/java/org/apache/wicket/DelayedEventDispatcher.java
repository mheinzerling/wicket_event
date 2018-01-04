package org.apache.wicket;

import com.mycompany.HomePage.Payload;
import java.util.LinkedList;
import java.util.Queue;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.event.IEventSink;
import org.apache.wicket.request.cycle.RequestCycle;

public class DelayedEventDispatcher implements IEventDispatcher
{
	private static MetaDataKey<DelayedEventDispatcher> KEY           = new MetaDataKey<DelayedEventDispatcher>()
	{
	};
	private final  Queue<Runnable>                     pendingEvents = new LinkedList<>();

	@Override
	public void dispatchEvent(final Object sink, final IEvent<?> event, final Component component)
	{
		if( component != null && sink instanceof IComponentAwareEventSink )
			pendingEvents.offer(() -> ((IComponentAwareEventSink) sink).onEvent(component, event));
		else if( sink instanceof IEventSink ) pendingEvents.offer(() -> ((IEventSink) sink).onEvent(event));
	}

	private void runPending()
	{
		while( !pendingEvents.isEmpty() ) pendingEvents.poll().run();
	}

	public static void send(final Component component, final IEventSink sink, final Broadcast broadcast, final Payload payload)
	{
		DelayedEventDispatcher delayedEventDispatcher = getDelayedEventDispatcher();
		new ComponentEventSender(component, delayedEventDispatcher).send(sink, broadcast, payload);
		delayedEventDispatcher.runPending();
	}

	private static DelayedEventDispatcher getDelayedEventDispatcher()
	{
		DelayedEventDispatcher delayedEventDispatcher = RequestCycle.get().getMetaData(KEY);
		if( delayedEventDispatcher == null )
		{
			delayedEventDispatcher = new DelayedEventDispatcher();
			RequestCycle.get().setMetaData(DelayedEventDispatcher.KEY, delayedEventDispatcher);
		}
		return delayedEventDispatcher;
	}
}
