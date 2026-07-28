export enum type {
    Person = 'Person',
    Company = 'Company'
}

export enum role {
    Admin = 'Admin',
    Customer = 'Customer'
}

export interface Customer {
    id: number;
    name: string;
    email: string;
    address: string;
    type: type;
    role: role;
    dob: string;
}