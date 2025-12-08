import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
// import { ProductListComponent } from '../../features/catalog/components/product-list/product-list';
// import { ProductFormComponent } from '../../features/catalog/components/product-form/product-form';

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    // ProductListComponent,  // Adicionaremos depois
    // ProductFormComponent   // Adicionaremos depois
  ],
  templateUrl: './catalog.html',
  styleUrl: './catalog.scss',
})
export class Catalog {
  showForm = false;

  toggleForm() {
    this.showForm = !this.showForm;
  }
}
