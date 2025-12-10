export type User = {
  id: string;
  email: string;
  name: string;
  age?: number;
  phone: string;
  passwordHash: string;
  role: UserRole;
  active: boolean;
  createdAt: Date;
  updatedAt: Date;
  productsIds?: string[];
};

type UserRole = "user" | "admin" | "seller";
