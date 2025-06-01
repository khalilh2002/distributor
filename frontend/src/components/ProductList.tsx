import React from 'react';
import { ProductDTO } from '../services/api';
import { useTranslation } from 'react-i18next'; 

interface ProductListProps {
    products: ProductDTO[];
    onSelect: (productId: number) => void;
}

const ProductList: React.FC<ProductListProps> = ({ products, onSelect }) => {
    const { t } = useTranslation(); 
    
    if (!products.length) {
        return <p className="text-center text-muted">{t('noProducts')}</p>;
    }

    return (
        <div className="mb-4">
            <h2 className="mb-3 display-6" id="products-heading">{t('productsHeading')}</h2>
            <div className="row row-cols-1 row-cols-sm-2 row-cols-lg-3 g-4">
                {products.map((product) => (
                    <div className="col" key={product.id}>
                        <div className={`card h-100 shadow-sm rounded-3 ${!product.purchasable ? 'border-secondary opacity-75' : 'border-primary'}`}>
                            <div className="card-body d-flex flex-column">
                                <h5 className="card-title text-truncate">{product.name}</h5>
                                <p className="card-text fw-bold fs-5 text-success mb-3">
                                    {product.price.toFixed(2)} {t('currencySymbol')}
                                </p>
                                
                                {product.purchasable ? (
                                    <button
                                        className="btn btn-primary w-100 mt-auto rounded-pill"
                                        onClick={() => onSelect(product.id)}
                                        aria-label={t('selectItemButtonAria', { itemName: product.name })} // Example for dynamic aria-label
                                    >
                                        {t('selectItemButton')}
                                    </button>
                                ) : (
                                    <button
                                        className="btn btn-outline-secondary w-100 mt-auto rounded-pill disabled"
                                        aria-disabled="true"
                                    >
                                        {t('cannotAffordButton')}
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