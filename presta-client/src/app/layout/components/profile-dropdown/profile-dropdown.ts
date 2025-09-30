import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterModule } from '@angular/router';

// PrimeNG
import { AvatarModule } from 'primeng/avatar';
import { MenuModule } from 'primeng/menu';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { MenuItem } from 'primeng/api';

// Models & Services
import { Client } from '../../../models/client.model';
import { Contractor } from '../../../models/contractor.model';
import { User } from '../../../models/user.model';
import { KeycloakService } from '../../../services/keycloak/keycloak';
import { Me, UserManagementService } from '../../../services/userManagement/user-management.service';

type UserProfile = Client | Contractor | KeycloakProfile;

interface KeycloakProfile {
  id: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  username?: string;
  roles: string[];
}

@Component({
  selector: 'app-profile-dropdown',
  standalone: true,
  imports: [
    CommonModule, RouterModule,
    AvatarModule, MenuModule, TagModule, ButtonModule
  ],
  templateUrl: './profile-dropdown.html',
})
export class ProfileDropdownComponent implements OnInit {
  private readonly keycloakService = inject(KeycloakService);
  private readonly userManagementService = inject(UserManagementService);
  private readonly router = inject(Router);

  // Signals
  readonly currentProfile = signal<Me | null>(null);
  readonly loading = signal(true);

  // Computed properties
  readonly profileType = computed(() => {
    const profile = this.currentProfile();
    if (!profile) return 'INCONNU';

    return profile.kind;
  });

  readonly profileTypeSeverity = computed(() => {
    const type = this.profileType();
    switch (type) {
      case 'ADMIN': return 'danger';
      case 'CONTRACTOR': return 'info';
      case 'CLIENT': return 'success';
      default: return 'secondary';
    }
  });

  readonly displayName = computed(() => {
    const type = this.profileType();
    if (!type || type === "INCONNU") return 'Utilisateur';

    const profile = this.currentProfile();
    if (!profile) return 'Utilisateur';

    switch (type) {
      case "ADMIN":
        const adminProfile = profile as any;
        return adminProfile.firstName && adminProfile.lastName
          ? `${adminProfile.firstName} ${adminProfile.lastName}`
          : adminProfile.username || adminProfile.email || 'Admin';

      case 'CONTRACTOR':
        // Profile est Contractor avec kind
        const contractorProfile = profile as any;
        return contractorProfile.fullName || 'Prestataire';

      case 'CLIENT':
        // Profile est Client avec kind
        const clientProfile = profile as any;
        return clientProfile.user?.profile?.fullName || 'Client';

      default:
        return 'Utilisateur';
    }
  });

  readonly avatarInitials = computed(() => {
    const name = this.displayName();
    if (!name || name === 'Utilisateur') return '?';

    const parts: string[] = name.trim().split(/\s+/).filter((p: string): boolean => p.length > 0);
    if (parts.length === 0) return '?';
    if (parts.length === 1) return parts[0][0].toUpperCase();

    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  });

  readonly avatarColorClass = computed(() => {
    const type = this.profileType();
    switch (type) {
      case 'ADMIN': return '!bg-gradient-to-br !from-red-500 !to-pink-600';
      case 'CONTRACTOR': return '!bg-gradient-to-br !from-blue-500 !to-purple-600';
      case 'CLIENT': return '!bg-gradient-to-br !from-green-500 !to-emerald-600';
      default: return '!bg-gradient-to-br !from-gray-500 !to-gray-600';
    }
  });

  readonly isActive = computed(() => {
    const profile = this.currentProfile();
    if (!profile) return false;

    if (profile.kind === "ADMIN") return true; // Admin toujours actif
    if ('user' in profile) return profile.user?.isActive ?? false;

    return false;
  });


  readonly userEmail = computed(() => {
    const profile = this.currentProfile();
    if (!profile) return null;

    if (profile.kind == "ADMIN") return profile.email;
    return profile.user?.contactInfo?.email;
  });

  readonly menuItems = computed<MenuItem[]>(() => {
    const profile = this.currentProfile();
    const type = this.profileType();
    
    const baseItems: MenuItem[] = [
      {
        label: 'Mon Profil',
        icon: 'pi pi-user',
        command: () => this.navigateToProfile()
      },
      { separator: true },
    ];

    // Actions spécifiques selon le type
    if (type === 'ADMIN') {
      baseItems.push(
        {
          label: 'Administration',
          icon: 'pi pi-cog',
          items: [
            { 
              label: 'Clients', 
              icon: 'pi pi-users',
              command: () => this.router.navigate(['/admin/client'])
            },
            { 
              label: 'Services', 
              icon: 'pi pi-wrench',
              command: () => this.router.navigate(['/admin/service'])
            },
            { 
              label: 'Préstataires', 
              icon: 'pi pi-file-edit',
              command: () => this.router.navigate(['/admin/contractor'])
            }
          ]
        },
        { separator: true }
      );
    } else if (type === 'CONTRACTOR') {
      baseItems.push(
        {
          label: 'Mes Missions',
          icon: 'pi pi-briefcase',
          command: () => this.router.navigate(['/contractor/missions'])
        },
        {
          label: 'Planning',
          icon: 'pi pi-calendar',
          command: () => this.router.navigate(['/contractor/planning'])
        },
        { separator: true }
      );
    } else if (type === 'CLIENT') {
      baseItems.push(
        {
          label: 'Mes Demandes',
          icon: 'pi pi-list',
          command: () => this.router.navigate(['/client/requests'])
        },
        {
          label: 'Historique',
          icon: 'pi pi-history',
          command: () => this.router.navigate(['/client/history'])
        },
        { separator: true }
      );
    }

    // Actions communes
    baseItems.push(
      {
        label: 'Déconnexion',
        icon: 'pi pi-sign-out',
        styleClass: 'text-red-500',
        command: () => this.keycloakService.logout()
      }
    );

    return baseItems;
  });
 async ngOnInit() {
    try {
      await this.loadCurrentProfile();
    } catch (error) {
      console.error('Erreur chargement profil:', error);
    } finally {
      this.loading.set(false);
    }
  }

  private async loadCurrentProfile(): Promise<void> {
    const isLoggedIn = await this.keycloakService.isLoggedIn();
    if (!isLoggedIn) return;
    this.currentProfile.set(null); // Réinitialiser avant de charger
    this.userManagementService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentProfile.set(user);
      },
      error: (err) => {
        console.error('Erreur récupération utilisateur courant:', err);
        this.currentProfile.set(null);
      }
    });


  }



  private navigateToProfile(): void {
    const type = this.profileType();
    const profile = this.currentProfile();
    
    if (!profile) return;

    switch (type) {
      case 'ADMIN':
        this.router.navigate(['/admin/'])
        break;
      case 'CONTRACTOR':
        this.router.navigate(['/contractor/profile'])
        break;
      case 'CLIENT':
        this.router.navigate(['/client/profile'])
        break;
    }
  }
}