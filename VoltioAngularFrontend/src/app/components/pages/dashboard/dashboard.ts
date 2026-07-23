import { Component } from '@angular/core';
import { Slider } from '../../shared-components/slider/slider';
import { Button } from '../../shared-components/button/button';

@Component({
  selector: 'app-dashboard',
  imports: [Slider, Button],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {}
