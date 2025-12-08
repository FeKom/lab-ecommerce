import { Injectable } from '@angular/core';

export interface Product {
  id: string;
  name: string;
  price: number;
  stock: number;
  category?: string;
  description?: string;
  tags?: string[];
  createdAt?: Date;
  updatedAt?: Date;
}

export interface ProductCreateRequest {
  name: string;
  price: number;
  stock: number;
  category: string;
  description: string;
  tags: string[];
}

@Injectable({
  providedIn: 'root',
})
export class Catalog {
  private apiUrl = "http://localhost:8080/api/products";
}
