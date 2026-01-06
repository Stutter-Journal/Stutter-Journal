import { Component } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';
import { FeatCascade } from '@org/feat-cascade';

@Component({
  selector: 'lib-feat-landing',
  imports: [NgOptimizedImage, FeatCascade],
  templateUrl: './feat-landing.html',
  styleUrl: './feat-landing.css',
})
export class FeatLanding {}
