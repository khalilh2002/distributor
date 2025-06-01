import React, { useState, useEffect, useCallback } from 'react';
// import './App.css'; // Remove if you don't have any App.css specific styles
import ProductList from './components/ProductList';
import CoinInserter from './components/CoinInserter';
import SelectedItems from './components/SelectedItems';
import * as api from './services/api';

function App() {
    const [products, setProducts] = useState<api.ProductDTO[]>([]);
    const [currentState, setCurrentState] = useState<api.StateResponse | null>(null);
    const [message, setMessage] = useState<string>('');
    const [error, setError] = useState<string>('');
    // const [selectedProductIdsForHighlight, setSelectedProductIdsForHighlight] = useState<Set<number>>(new Set()); // Not strictly needed with card design

    const clearMessages = () => {
        setMessage('');
        setError('');
    };
    
    const showTemporaryMessage = (msg: string, isError: boolean = false) => {
        if (isError) setError(msg);
        else setMessage(msg);
        setTimeout(() => {
            clearMessages();
        }, 4000); // Message disappears after 4 seconds
    };

    const loadInitialData = useCallback(async (showLoadingMessage: boolean = false) => {
        if(showLoadingMessage) setMessage('Loading data...');
        try {
            const prods = await api.fetchProducts();
            setProducts(prods);
            const state = await api.fetchCurrentState();
            setCurrentState(state);
            if(showLoadingMessage) clearMessages();
        } catch (err: any) {
            console.error("Error loading initial data:", err);
            const errorMessage = err.response?.data?.message || err.message || 'Failed to load data.';
            showTemporaryMessage(errorMessage, true);
        }
    }, []);

    useEffect(() => {
        loadInitialData(true);
    }, [loadInitialData]);

    const handleInsertCoin = async (value: number) => {
        clearMessages();
        try {
            const response = await api.insertCoin(value);
            showTemporaryMessage(`Coin inserted. New balance: ${response.currentBalance.toFixed(2)} MAD`);
            await loadInitialData();
        } catch (err: any) {
            const errorMessage = err.response?.data?.message || err.message || 'Failed to insert coin.';
            showTemporaryMessage(errorMessage, true);
        }
    };

    const handleSelectProduct = async (productId: number) => {
        clearMessages();
        try {
            const response = await api.selectProduct(productId);
            showTemporaryMessage(response.message || `Product action successful.`);
            await loadInitialData();
        } catch (err: any) {
            const errorMessage = err.response?.data?.message || err.message || 'Failed to select product.';
            showTemporaryMessage(errorMessage, true);
        }
    };

    const handleDeselectProduct = async (productId: number) => {
        clearMessages();
        try {
            const response = await api.deselectProduct(productId);
            showTemporaryMessage(response.message || `Product deselected.`);
            await loadInitialData();
        } catch (err: any)
        {
            const errorMessage = err.response?.data?.message || err.message || 'Failed to deselect product.';
            showTemporaryMessage(errorMessage, true);
        }
    };

    const handleDispense = async () => {
        clearMessages();
        if (!currentState || currentState.selectedProducts.length === 0) {
            showTemporaryMessage("Please select items to dispense.", true);
            return;
        }
        try {
            const response = await api.dispenseItems();
            let dispenseMsg = response.message || "Dispense successful!";
            if (response.dispensedProducts && response.dispensedProducts.length > 0) {
                dispenseMsg += ` Dispensed: ${response.dispensedProducts.map(p => p.name).join(', ')}.`;
            }
            if (response.changeCoins && response.changeCoins.length > 0) {
                dispenseMsg += ` Change: ${response.changeCoins.map(c => c.toFixed(2)).join(', ')} MAD.`;
            }
            showTemporaryMessage(dispenseMsg);
            await loadInitialData();
        } catch (err: any) {
            const errorMessage = err.response?.data?.message || err.message || 'Failed to dispense items.';
            showTemporaryMessage(errorMessage, true);
        }
    };

    const handleCancel = async () => {
        clearMessages();
        try {
            const response = await api.cancelTransaction();
            showTemporaryMessage(response.message || "Transaction cancelled and coins refunded.");
            await loadInitialData();
        } catch (err: any) {
            const errorMessage = err.response?.data?.message || err.message || 'Failed to cancel transaction.';
            showTemporaryMessage(errorMessage, true);
        }
    };

    return (
        <main className="container py-4 py-lg-5" role="main">
            <header className="text-center mb-4 mb-lg-5">
                <h1 className="display-4 fw-bold text-primary">Zenika Vends</h1>
                <p className="lead text-muted">Your convenient automated snack and drink solution.</p>
            </header>

            {error && <div className="alert alert-danger shadow-sm rounded-3" role="alert">{error}</div>}
            {message && <div className="alert alert-success shadow-sm rounded-3" role="status">{message}</div>}

            <div className="row g-4 g-lg-5">
                <section className="col-lg-7" aria-labelledby="products-heading">
                    <ProductList
                        products={products}
                        onSelect={handleSelectProduct}
                    />
                </section>

                <aside className="col-lg-5">
                    <CoinInserter onInsertCoin={handleInsertCoin} />

                    <div className="card shadow-sm rounded-3 mb-4">
                        <div className="card-body">
                            <h4 className="card-title mb-3 text-primary">Transaction</h4>
                            <div className="d-flex justify-content-between align-items-center mb-3">
                                <span className="fs-5 text-muted">Current Balance:</span>
                                <span className="fs-3 fw-bold text-success">
                                    {currentState?.currentBalance.toFixed(2) || '0.00'} MAD
                                </span>
                            </div>
                            <hr/>
                            <SelectedItems
                                selectedItems={currentState?.selectedProducts || []}
                                onDeselect={handleDeselectProduct}
                                totalCost={currentState?.totalSelectedCost || 0}
                            />
                        </div>
                    </div>

                    <div className="d-grid gap-3">
                        <button 
                            className="btn btn-lg btn-success rounded-pill shadow-sm" 
                            onClick={handleDispense} 
                            disabled={!currentState || !currentState.selectedProducts || currentState.selectedProducts.length === 0}
                            aria-label="Dispense selected items"
                        >
                            Dispense Items
                        </button>
                        <button 
                            className="btn btn-lg btn-outline-danger rounded-pill shadow-sm" 
                            onClick={handleCancel}
                            aria-label="Cancel transaction and refund money"
                        >
                            Cancel & Refund
                        </button>
                    </div>
                </aside>
            </div>
             <footer className="text-center text-muted mt-5 py-3 border-top">
                <p>© {new Date().getFullYear()} Zenika Vending Solutions</p>
            </footer>
        </main>
    );
}

export default App;