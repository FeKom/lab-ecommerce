export type User = {
  id: string;
  email: string;
  emailVerified: boolean;
  name: string;
  age?: number;
  phone: string;
  password: string;
  role: UserRole;
  image?: string;
  active: boolean;
  createdAt: Date;
  updatedAt: Date;
};

type UserRole = "user" | "admin" | "seller";
