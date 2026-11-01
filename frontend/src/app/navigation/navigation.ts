import { Component } from "@angular/core";
import { RouterLink } from "@angular/router";

@Component({
  selector: 'navigation',
  templateUrl: './navigation.html',
  styleUrl: './navigation.css',
  imports: [RouterLink]
})
export default class Navigation { }
