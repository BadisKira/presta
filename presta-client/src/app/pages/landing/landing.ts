import { Component } from '@angular/core';
import { FooterComponent} from './components/footer.component';
import { TopbarComponent } from './components/topbar.component';
import { HeroComponent } from './components/hero.component';

@Component({
  selector: 'app-landing',
  imports: [TopbarComponent,HeroComponent,FooterComponent],
  templateUrl: './landing.html',
  styleUrl: './landing.scss'
})
export class LandingPage {

}
