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
    id: string;
    user_id: string;
    name: string;
    number: number;
    street: string;
    state: string;
    zip_code: string;
    country: string;
    complement: string | null;
    active: boolean;
    created_at: ColumnType<Date, string | undefined, never>;
    updated_at: ColumnType<Date, string | undefined, string>;
}

export type Address = Selectable<AddressTable>;
export type NewAddress = Insertable<AddressTable>;
export type CommerceUpdate = Updateable<AddressTable>