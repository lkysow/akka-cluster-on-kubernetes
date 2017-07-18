package com.hootsuite.akkak8s

import akka.actor.Actor

class Backend extends Actor {
  def receive = {
    case NotHotDogRequest(message) =>
      if (message.toLowerCase.contains("hotdog")) {
        sender() ! NotHotDogResponse(true)
      } else {
        sender() ! NotHotDogResponse(false)
      }
  }
}
