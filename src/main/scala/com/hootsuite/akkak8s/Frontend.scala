package com.hootsuite.akkak8s

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

class Frontend(backendRouter: ActorRef) extends Actor with ActorLogging {
  def receive = {
    case r: NotHotDogRequest =>
      backendRouter.forward(r)
  }
}

object Frontend {
  def props(backendRouter: ActorRef): Props = Props(new Frontend(backendRouter))
}
