package com.example

import com.example.domain.Team
import com.example.domain.Person

trait HelloTeam {
  val team: Team = Team(name = "Winners", members = Seq(Person(name = "John", age = 19)))
}
