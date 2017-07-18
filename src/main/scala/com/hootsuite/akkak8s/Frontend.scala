package com.hootsuite.akkak8s

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

class Frontend(backend: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case r: NotHotDogRequest =>
      backend.forward(r)
  }
}

object Frontend {
  def props(backend: ActorRef): Props = Props(new Frontend(backend))
}
