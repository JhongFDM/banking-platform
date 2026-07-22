import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { Offer } from '../../../models/offer';
import { Badge } from '../badge/badge';

@Component({
  selector: 'app-slider',
  imports: [Badge],
  templateUrl: './slider.html',
  styleUrl: './slider.css',
})
export class Slider {

  offers: Offer[] = [
    {
      title: 'Zero-Fee Starter Account',
      description: 'Open your first account with no monthly maintenance fee for the first 12 months.',
      badge: 'New',
      image: '../../assets/images/image_1.jpg'
    },
    {
      title: 'Smart Savings Boost',
      description: 'Earn up to 4.10% variable APY with automated monthly round-up deposits.',
      badge: 'Popular',
      image: '../../assets/images/image_2.jpg'
    },
    {
      title: 'Travel Rewards Card',
      description: 'Get 2x points on flights and hotels, plus no foreign transaction fees.',
      badge: 'Limited Offer',
      image: '../../assets/images/image_3.jpg'
    }
  ];

  sliderImages: string [] = ['../../assets/images/image_1.jpg', '../../assets/images/image_2.jpg', '../../assets/images/image_3.jpg'];
  currentIndex = signal(0);
  private autoSlideTimer: any;

  ngOnInit() {
    this.startAutoSlide();
  }

  ngOnDestroy() {
    this.stopAutoSlide(); 
  }

  startAutoSlide() {
    this.autoSlideTimer = setInterval(() => {
      this.next();
    }, 3000); 
  }

  stopAutoSlide() {
    if (this.autoSlideTimer) {
      clearInterval(this.autoSlideTimer);
    }
  }

  next() {
    this.currentIndex.update(val => (val + 1) % this.offers.length);
  }

  prev() {
    this.currentIndex.update(val => (val - 1 + this.offers.length) % this.offers.length);
  }

}