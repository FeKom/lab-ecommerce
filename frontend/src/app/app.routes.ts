import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home';
import { Catalog } from './pages/catalog/catalog';

export const routes: Routes = [
  {
    path: "",
    component: HomeComponent,
  },
  {
    path: "/home",
    component: HomeComponent,
  },
  {
    path: "catalog",
   component: Catalog,
  },
  {
    path: "**", redirectTo: ""
  }
];
