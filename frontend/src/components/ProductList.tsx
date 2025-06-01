import React from 'react';
import { ProductDTO } from '../services/api'; // Assuming api.ts is in ../services/

interface ProductListProps {
    products: ProductDTO[];
    onSelect: (productId: number) => void;
    // selectedProductIds: Set<number>; // We can derive visual cues differently if needed
                                    // Or keep it if App.tsx manages complex selection states
}

const ProductList: React.FC<ProductListProps> = ({ products, onSelect }) => {
    if (!products.length) {
        return <p className="text-center text-muted">No products available at the moment.</p>;
    }

    return (
        <div className="mb-4">
            <h2 className="mb-3 display-6">Available Products</h2>
            <div className="row row-cols-1 row-cols-sm-2 row-cols-lg-3 g-4"> {/* Responsive grid */}
                {products.map((product) => (
                    <div className="col" key={product.id}>
                        <div className={`card h-100 shadow-sm rounded-3 ${!product.purchasable ? 'border-secondary opacity-75' : 'border-primary'}`}>
                            <div className="card-body d-flex flex-column">
                                <h5 className="card-title text-truncate">{product.name}</h5>
                                <p className="card-text fw-bold fs-5 text-success mb-3">
                                    {product.price.toFixed(2)} MAD
                                </p>
                                
                                {product.purchasable ? (
                                    <button
                                        className="btn btn-primary w-100 mt-auto rounded-pill" // mt-auto pushes button to bottom
                                        onClick={() => onSelect(product.id)}
                                        aria-label={`Select ${product.name}`}
                                    >
                                        Select Item
                                    </button>
                                ) : (
                                    <button
                                        className="btn btn-outline-secondary w-100 mt-auto rounded-pill disabled"
                                        aria-disabled="true"
                                    >
                                        Cannot Afford
                                    </button>
                                )}
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default ProductList;