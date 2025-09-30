import { Component, inject } from '@angular/core';
import { ButtonModule } from "primeng/button";
import { CommonModule } from '@angular/common';
import { LayoutService } from '../../layout.service';
@Component({
    selector: 'darkmode-button-component',
    imports: [ButtonModule, CommonModule],
    template: `
    <button type="button" class="layout-topbar-action" (click)="toggleDarkMode()">
        <i [ngClass]="{ 'pi ': true, 'pi-moon': layoutService.isDarkTheme(), 'pi-sun': !layoutService.isDarkTheme() }"></i>
    </button>
  `
})
export class DarkmodeButtonComponent {

    layoutService = inject(LayoutService);
    toggleDarkMode() {
        this.layoutService.layoutConfig.update((state) => ({ ...state, darkTheme: !state.darkTheme }));
        // this.layoutService.toggleDarkMode();
    }
}