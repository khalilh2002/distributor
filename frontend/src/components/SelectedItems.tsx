import React from 'react';
import { StateResponse } from '../services/api';

interface SelectedItemsProps {
    selectedItems: StateResponse['selectedProducts'];
    onDeselect: (productId: number) => void;
    totalCost: number;
}

const SelectedItems: React.FC<SelectedItemsProps> = ({ selectedItems, onDeselect, totalCost }) => {
    return (
        <div className="mb-3">
            <h5 className="mb-2 text-dark">Your Selection</h5>
            {(!selectedItems || selectedItems.length === 0) ? (
                <p className="text-muted fst-italic">No items selected yet.</p>
            ) : (
                <ul className="list-group list-group-flush mb-3">
                    {selectedItems.map((item) => (
                        <li key={`${item.id}-${item.name}-${item.quantity}`} // More stable key
                            className="list-group-item d-flex justify-content-between align-items-center px-0 py-2">
                            <div>
                                <span className="fw-medium">{item.name}</span>
                                <small className="text-muted d-block">
                                    (x{item.quantity}) @ {item.price.toFixed(2)} MAD each
                                </small>
                            </div>
                            <button 
                                className="btn btn-sm btn-outline-warning rounded-circle p-0" // Circle button
                                style={{ width: '30px', height: '30px', lineHeight: '1' }} // Inline style for simple circle
                                onClick={() => onDeselect(item.id)}
                                aria-label={`Deselect one ${item.name}`}
                            >
                                − {/* Minus sign */}
                            </button>
                        </li>
                    ))}
                </ul>
            )}
            <p className="h5 text-end">
                Total: <span className="fw-bold text-success">{totalCost.toFixed(2)} MAD</span>
            </p>
        </div>
    );
};

export default SelectedItems;