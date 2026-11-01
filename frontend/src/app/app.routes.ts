import { Routes } from '@angular/router';
import { moviesPages } from './movies/routes';
import { ScreensRoute } from './screens/screen-list';
import { ScheduleRoute } from './schedule/schedlule';

export const routes: Routes = [
  ...moviesPages,
  ScreensRoute,
  ScheduleRoute,
];
