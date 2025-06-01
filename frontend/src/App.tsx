import React, { useState, useEffect, useCallback } from 'react';
import ProductList from './components/ProductList';
import CoinInserter from './components/CoinInserter';
import SelectedItems from './components/SelectedItems';
import * as api from './services/api';
import { useTranslation } from 'react-i18next';

function App() {
    const { t, i18n } = useTranslation();

    const [products, setProducts] = useState<api.ProductDTO[]>([]);
    const [currentState, setCurrentState] = useState<api.StateResponse | null>(null);
    const [message, setMessage] = useState<string>('');
    const [error, setError] = useState<string>('');

    // Moved clearMessages to be defined before it's used
    const clearMessages = () => {
        setMessage('');
        setError('');
    };

    const showTemporaryMessage = (msgKey: string, options?: object, isError: boolean = false) => {
        const translatedMsg = t(msgKey, options);
        if (isError) setError(translatedMsg);
        else setMessage(translatedMsg);
        setTimeout(() => {
            clearMessages();
        }, 4000);
    };

    const loadInitialData = useCallback(async (showLoadingMsg: boolean = false) => {
        if (showLoadingMsg) showTemporaryMessage('messages.loading');
        try {
            const prods = await api.fetchProducts();
            setProducts(prods);
            const state = await api.fetchCurrentState();
            setCurrentState(state);
            if (showLoadingMsg) clearMessages(); // Clear loading message after success
        } catch (err: any) {
            console.error("Error loading initial data:", err);
            const apiErrorMessage = err.response?.data?.message || err.message;
            const errorKey = apiErrorMessage && resourcesContainKey(apiErrorMessage) ? apiErrorMessage : 'messages.errorLoadData';
            showTemporaryMessage(errorKey, apiErrorMessage && !resourcesContainKey(apiErrorMessage) ? { customMessage: apiErrorMessage } : {}, true);
        }
    }, [t]); // Added t as dependency because showTemporaryMessage uses it

    // Helper to check if a string is a key in our translation resources (simplistic check)
    const resourcesContainKey = (key: string): boolean => {
        return t(key) !== key || key.startsWith('messages.'); 
    };


    useEffect(() => {
        loadInitialData(true);
    }, [loadInitialData]);

    const changeLanguage = (lng: string) => {
      i18n.changeLanguage(lng);
     
    };

    const handleInsertCoin = async (value: number) => {
        clearMessages();
        try {
            const response = await api.insertCoin(value);
            showTemporaryMessage('messages.coinInserted', {
                balance: response.currentBalance.toFixed(2),
                currency: t('currencySymbol')
            });
            await loadInitialData();
        } catch (err: any) {
            const apiErrorMessage = err.response?.data?.message || err.message;
            const errorKey = apiErrorMessage && resourcesContainKey(apiErrorMessage) ? apiErrorMessage : 'messages.errorInsertCoin';
            showTemporaryMessage(errorKey, apiErrorMessage && !resourcesContainKey(apiErrorMessage) ? { customMessage: apiErrorMessage } : {}, true);
        }
    };

    // Define handleSelectProduct before it's used in JSX
    const handleSelectProduct = async (productId: number) => {
        clearMessages();
        try {
            const response = await api.selectProduct(productId);
            // If the API message is a key, translate it, otherwise display directly
            const messageKeyOrText = response.message || 'messages.productActionSuccess';
            const displayMessage = resourcesContainKey(messageKeyOrText) ? t(messageKeyOrText, { productName: response.product?.name }) : messageKeyOrText;
            setMessage(displayMessage); 
             setTimeout(() => { clearMessages(); }, 4000);

            await loadInitialData();
        } catch (err: any) {
            const apiErrorMessage = err.response?.data?.message || err.message;
            const errorKey = apiErrorMessage && resourcesContainKey(apiErrorMessage) ? apiErrorMessage : 'messages.errorSelectProduct';
            showTemporaryMessage(errorKey, apiErrorMessage && !resourcesContainKey(apiErrorMessage) ? { customMessage: apiErrorMessage } : {}, true);
        }
    };

    const handleDeselectProduct = async (productId: number) => {
        clearMessages();
        try {
            const response = await api.deselectProduct(productId);
            const messageKeyOrText = response.message || 'messages.productActionSuccess'; // Default if API gives no message
            const displayMessage = resourcesContainKey(messageKeyOrText) ? t(messageKeyOrText, { productName: response.product?.name }) : messageKeyOrText;
            setMessage(displayMessage);
            setTimeout(() => { clearMessages(); }, 4000);

            await loadInitialData();
        }
        catch (err: any) {
            const apiErrorMessage = err.response?.data?.message || err.message;
            const errorKey = apiErrorMessage && resourcesContainKey(apiErrorMessage) ? apiErrorMessage : 'messages.errorDeselectProduct';
            showTemporaryMessage(errorKey, apiErrorMessage && !resourcesContainKey(apiErrorMessage) ? { customMessage: apiErrorMessage } : {}, true);
        }
    };


    const handleDispense = async () => {
        clearMessages();
        if (!currentState || !currentState.selectedProducts || currentState.selectedProducts.length === 0) {
            showTemporaryMessage('dispenseDisabledTooltip', {}, true);
            return;
        }
        try {
            const response = await api.dispenseItems();
            const dispensedProductNames = response.dispensedProducts?.map(p => p.name).join(', ') || '';
            const changeCoinsFormatted = response.changeCoins?.map(c => c.toFixed(2)).join(', ') || '';
            const currency = t('currencySymbol');

            let finalMessage = "";
            if (response.message && resourcesContainKey(response.message)) { // If API message is a key
                finalMessage = t(response.message);
            } else if (response.message) { // If API message is direct text
                finalMessage = response.message;
            } else { // Default
                finalMessage = t('messages.dispenseSuccess');
            }

            if (dispensedProductNames && changeCoinsFormatted) {
                 finalMessage += " " + t('messages.dispenseDetails', { products: dispensedProductNames, change: changeCoinsFormatted, currency: currency });
            } else if (dispensedProductNames) {
                 finalMessage += " " + t('messages.dispenseNoChange', { products: dispensedProductNames });
            }
            
            setMessage(finalMessage);
            setTimeout(() => { clearMessages(); }, 6000); // Longer time for dispense message

            await loadInitialData();
        } catch (err: any) {
            const apiErrorMessage = err.response?.data?.message || err.message;
            const errorKey = apiErrorMessage && resourcesContainKey(apiErrorMessage) ? apiErrorMessage : 'messages.errorDispense';
            showTemporaryMessage(errorKey, apiErrorMessage && !resourcesContainKey(apiErrorMessage) ? { customMessage: apiErrorMessage } : {}, true);
        }
    };

    const handleCancel = async () => {
        clearMessages();
        try {
            const response = await api.cancelTransaction();
            const messageKeyOrText = response.message || 'messages.cancelSuccess';
            const displayMessage = resourcesContainKey(messageKeyOrText) ? t(messageKeyOrText) : messageKeyOrText;
            setMessage(displayMessage);
            setTimeout(() => { clearMessages(); }, 4000);
            await loadInitialData();
        } catch (err: any) {
            const apiErrorMessage = err.response?.data?.message || err.message;
            const errorKey = apiErrorMessage && resourcesContainKey(apiErrorMessage) ? apiErrorMessage : 'messages.errorCancel';
            showTemporaryMessage(errorKey, apiErrorMessage && !resourcesContainKey(apiErrorMessage) ? { customMessage: apiErrorMessage } : {}, true);
        }
    };

    return (
        <main className="container py-4 py-lg-5" role="main">
            <div className="text-end mb-3">
                <div className="btn-group" role="group" aria-label="Language selection">
                    <button type="button" className={`btn btn-sm ${i18n.language === 'en' ? 'btn-primary' : 'btn-outline-secondary'}`} onClick={() => changeLanguage('en')}>English</button>
                    <button type="button" className={`btn btn-sm ${i18n.language === 'fr' ? 'btn-primary' : 'btn-outline-secondary'}`} onClick={() => changeLanguage('fr')}>Français</button>
                </div>
            </div>

            <header className="text-center mb-4 mb-lg-5">
                <h1 className="display-4 fw-bold text-primary">{t('vendingMachineTitle')}</h1>
                <p className="lead text-muted">{t('vendingMachineSubtitle')}</p>
            </header>

            {error && <div className="alert alert-danger shadow-sm rounded-3" role="alert">{error}</div>}
            {message && <div className="alert alert-success shadow-sm rounded-3" role="status">{message}</div>}

            <div className="row g-4 g-lg-5">
                <section className="col-lg-7" aria-labelledby="products-heading">
                    <ProductList
                        products={products}
                        onSelect={handleSelectProduct} // Now correctly referenced
                    />
                </section>

                <aside className="col-lg-5">
                    <CoinInserter onInsertCoin={handleInsertCoin} />

                    <div className="card shadow-sm rounded-3 mb-4">
                        <div className="card-body">
                            <h4 className="card-title mb-3 text-primary">{t('transactionTitle')}</h4>
                            <div className="d-flex justify-content-between align-items-center mb-3">
                                <span className="fs-5 text-muted">{t('currentBalanceLabel')}</span>
                                <span className="fs-3 fw-bold text-success">
                                    {currentState?.currentBalance.toFixed(2) || '0.00'} {t('currencySymbol')}
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
                            aria-label={t('dispenseButton')}
                        >
                            {t('dispenseButton')}
                        </button>
                        <button
                            className="btn btn-lg btn-outline-danger rounded-pill shadow-sm"
                            onClick={handleCancel}
                            aria-label={t('cancelButton')}
                        >
                            {t('cancelButton')}
                        </button>
                    </div>
                </aside>
            </div>
             <footer className="text-center text-muted mt-5 py-3 border-top">
                <p dangerouslySetInnerHTML={{ __html: t('footerCopyright', { year: new Date().getFullYear() }) }} />
            </footer>
        </main>
    );
}

export default App;