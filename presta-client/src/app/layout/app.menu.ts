
import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { AppMenuitem } from './app.menuitem';
import { AppLogout } from "./app.logout";

@Component({
    selector: 'app-menu',
    imports: [CommonModule, AppMenuitem, RouterModule, AppLogout],
    template: `
        <ul class="layout-menu relative">
            <ng-container *ngFor="let item of model(); let i = index">
                <li app-menuitem *ngIf="!item.separator" [item]="item" [index]="i" [root]="true"></li>
                <li *ngIf="item.separator" class="menu-separator"></li>
            </ng-container>
            <div class="absolute top-0 w-10/12 mx-auto">
                <app-logout />
            </div>
        </ul>
    `
})
export class AppMenu {
    model = input.required<MenuItem[]>();
}