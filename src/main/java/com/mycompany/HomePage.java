package com.mycompany;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.event.IEventSink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class HomePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	public HomePage(final PageParameters parameters)
	{
		super(parameters);
		add(new PayloadLabel("c1")
		{

			@Override
			void onPayloadEvent(final Payload event)
			{

				System.out.println("c1 received: " + event.toString());
				setDefaultModelObject(event.toString());
				event.target.add(this);
			}
		});
		add(new PayloadLabel("c2")
		{
			@Override
			void onPayloadEvent(final Payload event)
			{
				System.out.println("c2 received: " + event.toString());
				setDefaultModelObject(event.toString());
				event.target.add(this);
				new Payload("2: Cascade", event.target).send(this, getPage());
			}
		});

		add(new PayloadLabel("c3")
		{
			void onPayloadEvent(final Payload event)
			{
				System.out.println("c3 received: " + event.toString());
				setDefaultModelObject(event.toString());
				event.target.add(this);
			}
		});

		add(new AjaxLink<Component>("submit")
		{
			@Override
			public void onClick(final AjaxRequestTarget target)
			{
				new Payload("1: Click", target).send(this, getPage());
			}
		});
	}

	public abstract static class PayloadLabel extends Label
	{

		PayloadLabel(final String id)
		{
			super(id, "nothing");
			setOutputMarkupId(true);
			add(new Behavior()
			{

				@Override
				public void onEvent(final Component component, final IEvent<?> event)
				{
					super.onEvent(component, event);
					if( event.getPayload() instanceof Payload )
					{
						if( event.getSource().equals(component) ) return; //skip self
						System.out.println(getId() + " behavior received: " + event.getPayload().toString());
					}
				}
			});
		}

		public void onEvent(final IEvent<?> event)
		{
			super.onEvent(event);
			if( event.getPayload() instanceof Payload )
			{
				if( event.getSource().equals(this) ) return; //skip self
				onPayloadEvent((Payload) event.getPayload());
			}
		}

		abstract void onPayloadEvent(final Payload event);
	}

	public static class Payload
	{
		String            value;
		AjaxRequestTarget target;

		Payload(final String value, final AjaxRequestTarget target)
		{
			this.value = value;
			this.target = target;
		}

		public void send(Component component, final IEventSink sink)
		{
			System.out.println("Send " + value + " Event");
			component.send(sink, Broadcast.BREADTH, this);
		}

		@Override
		public String toString()
		{
			return value;
		}
	}
}
