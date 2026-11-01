import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import Navigation from './navigation/navigation'

@Component({
  imports: [RouterOutlet, Navigation],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('frontend');
}
