import { Component } from "@angular/core";
import { Route } from "@angular/router";


@Component({
  selector: 'schedule',
  templateUrl: './schedule.html',
  styleUrl: './schedule.css',

})
class Schedule { }

export const ScheduleRoute: Route = {
  path: 'schedule',
  component: Schedule,
  title: 'Schedule'
}

