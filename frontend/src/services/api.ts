import axios from 'axios';

const API_URL = 'http://localhost:8080/api/distributor'; // Your backend URL

export interface Product {
    id: number;
    name: string;
    price: number;
}

export interface ProductDTO {
    id: number;
    name: string;
    price: number;
    purchasable: boolean;
}

export interface SelectionRequest {
    productId: number;
}

export interface CoinInsertRequest {
    value: number; // Or string if your backend expects "0.50"
}

export interface StateResponse {
    currentBalance: number;
    selectedProducts: Array<{ id: number; name: string; price: number; quantity: number }>;
    totalSelectedCost: number;
}

export interface DispenseResponseDTO { // Renamed to avoid conflict with component
    dispensedProducts: Product[];
    changeCoins: number[];
    message: string;
}


export const fetchProducts = async (): Promise<ProductDTO[]> => {
    const response = await axios.get<ProductDTO[]>(`${API_URL}/products`);
    return response.data;
};

export const insertCoin = async (coinValue: number) => {
    const response = await axios.post<{ currentBalance: number }>(`${API_URL}/coin`, { value: coinValue });
    return response.data;
};

export const selectProduct = async (productId: number) => {
    const response = await axios.post(`${API_URL}/select`, { productId });
    return response.data; // Structure this based on your actual API response
};

export const deselectProduct = async (productId: number) => {
    const response = await axios.post(`${API_URL}/deselect`, { productId });
    return response.data; // Structure this based on your actual API response
};

export const dispenseItems = async (): Promise<DispenseResponseDTO> => {
    const response = await axios.post<DispenseResponseDTO>(`${API_URL}/dispense`);
    return response.data;
};

export const cancelTransaction = async () => {
    const response = await axios.post(`${API_URL}/cancel`);
    return response.data;
};

export const fetchCurrentState = async (): Promise<StateResponse> => {
    const response = await axios.get<StateResponse>(`${API_URL}/state`);
    return response.data;
};