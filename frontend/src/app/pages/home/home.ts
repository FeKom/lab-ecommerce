import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class HomeComponent {
  title = "Ecommerce  Lab";
  features = [
    {
      title: "Cátalogo de Produtos",
      description: "Todos os produtos em um só lugar para fácil gerenciamento.",
      icon: "📦",
      color: "blue",
    },
    {
      title: "Cadastre seus Produtos",
      description: "Adicione novos produtos ao seu cátalogo com facilidade.",
      icon: "📦",
      route:"/catalog",
      color: "cyan",
    },
    {
      title: "Busca avançada",
      description: "Encontre produtos rapidamente com nossa funcionalidade de busca avançada.",
      icon: "🔍",
      route: "/search",
      color:"green",
    }
  ]

}
