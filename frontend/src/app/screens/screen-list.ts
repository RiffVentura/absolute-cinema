import { Component } from "@angular/core";
import { Route } from "@angular/router";

@Component({
  selector: 'screen-list',
  templateUrl: './screen-list.html',
  styleUrl: './screen-list.css',
})
class ScreenList { }

export const ScreensRoute: Route = {
  path: 'screens',
  component: ScreenList,
  title: 'Screens'
}
