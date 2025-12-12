import {
ColumnType,
Generated,
Insertable,
Selectable,
Updateable,
} from "kysely";

export interface Database {
address: AddressTable;
}

export interface AddressTable {
    id: Generated<string>;
    userId: string
    street: string;
    number: string;
    complement: string
    city: string;
    state: string;
    zipCode: string;
    createdAt: ColumnType<Date, string | undefined, never>
    updatedAt: ColumnType<Date, string | undefined, string>

}

export type Address = Selectable<AddressTable>;
export type NewAddress = Insertable<AddressTable>;
export type CommerceUpdate = Updateable<AddressTable>