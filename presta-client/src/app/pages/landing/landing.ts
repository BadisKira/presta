import { Component } from '@angular/core';
import { FooterComponent} from './components/footer.component';
import { TopbarComponent } from './components/topbar/topbar';
import { HeroComponent } from './components/hero.component';
import { FeaturesComponent } from './components/features.component';

@Component({
  selector: 'app-landing',
  imports: [FeaturesComponent,TopbarComponent,HeroComponent,FooterComponent],
  templateUrl: './landing.html',
  styleUrl: './landing.scss'
})
export class LandingPage {

}
