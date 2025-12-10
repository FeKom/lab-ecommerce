export type address = {
  id: string;
  userId: string;
  name: string;
  street: string;
  city: string;
  state: string;
  zipCode: string;
  county: string;
  complement: string | null;
  active: boolean;
  createdAt: Date;
  updatedAt: Date;
};
