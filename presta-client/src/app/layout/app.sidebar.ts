import { Component, ElementRef, input, OnInit, signal } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { AppMenu } from './app.menu';
import { UserRoles } from '../models/user.model';

@Component({
    selector: 'app-sidebar',
    imports: [AppMenu],
    template: `
        <div class="layout-sidebar">
            <app-menu [model]="model()"></app-menu>
        </div>
    `
})
export class AppSidebar implements OnInit {

    menuTypeByRole = input.required<UserRoles>();
    model = signal<MenuItem[]>([]);

    constructor(public el: ElementRef) {}
    ngOnInit(): void {
        switch (this.menuTypeByRole()) {
            case 'ADMIN':
                this.model.set(this.adminMenu);
                break;
            case 'CLIENT':
                this.model.set(this.clientMenu);
                break;
            case 'CONTRACTOR':
                this.model.set(this.contractorMenu);
                break;
            default:
                this.model.set([]);
                break;
        }
    }

    private adminMenu:MenuItem[] = [
        {
            label: 'Dashboard',
            items: [
                { label: 'Statistiques', icon: 'pi pi-fw pi-chart-bar', routerLink: [''] },
            ]
        },
        {
            label: 'Sections',
            items: [
                { label: 'Services', icon: 'pi pi-fw pi-briefcase', routerLink: ['service'] },
                { label: 'Clients', icon: 'pi pi-fw pi-user', routerLink: ['client'] },
                { label: 'Prestataires', icon: 'pi pi-fw pi-hammer', routerLink: ['prestataire'] },
            ]
        },
    ];


    private clientMenu:MenuItem[] = [
       {
            label: 'Client',
            items: [
                { label: 'nada', icon: 'pi pi-fw pi-home', routerLink: [''] },
            ]
        }
    ];

    private contractorMenu:MenuItem[] = [
        {
            label: 'Contractor',
            items: [
                { label: 'nada', icon: 'pi pi-fw pi-home', routerLink: [''] },
            ]
        }
    ];
}