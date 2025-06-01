import React from 'react';
import { StateResponse } from '../services/api';
import { useTranslation } from 'react-i18next';

interface SelectedItemsProps {
    selectedItems: StateResponse['selectedProducts'];
    onDeselect: (productId: number) => void;
    totalCost: number;
}

const SelectedItems: React.FC<SelectedItemsProps> = ({ selectedItems, onDeselect, totalCost }) => {
    const { t } = useTranslation();

    return (
        <div className="mb-3">
            <h5 className="mb-2 text-dark">{t('selectedItemsTitle')}</h5>
            {(!selectedItems || selectedItems.length === 0) ? (
                <p className="text-muted fst-italic">{t('noItemsSelected')}</p>
            ) : (
                <ul className="list-group list-group-flush mb-3">
                    {selectedItems.map((item) => (
                        <li key={`${item.id}-${item.name}-${item.quantity}`}
                            className="list-group-item d-flex justify-content-between align-items-center px-0 py-2">
                            <div>
                                <span className="fw-medium">{item.name}</span>
                                <small className="text-muted d-block">
                                    {t('itemQuantityPrice', { quantity: item.quantity, price: item.price.toFixed(2) })}
                                </small>
                            </div>
                            <button
                                className="btn btn-sm btn-outline-warning rounded-circle p-0"
                                style={{ width: '30px', height: '30px', lineHeight: '1' }}
                                onClick={() => onDeselect(item.id)}
                                aria-label={t('deselectButtonLabel', { itemName: item.name })}
                            >
                                −
                            </button>
                        </li>
                    ))}
                </ul>
            )}
            <p className="h5 text-end">
                {t('totalCostLabel')}{' '}
                <span className="fw-bold text-success">{totalCost.toFixed(2)} {t('currencySymbol')}</span>
            </p>
        </div>
    );
};

export default SelectedItems;