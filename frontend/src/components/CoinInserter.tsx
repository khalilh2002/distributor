import React from 'react';
import { useTranslation } from 'react-i18next';

interface CoinInserterProps {
    onInsertCoin: (value: number) => void;
}

const CoinInserter: React.FC<CoinInserterProps> = ({ onInsertCoin }) => {
    const { t } = useTranslation();
    const validCoins = [0.5, 1.0, 2.0, 5.0, 10.0];

    return (
        <div className="card shadow-sm rounded-3 mb-4">
            <div className="card-body">
                <h5 className="card-title mb-3 text-primary">{t('insertCoinTitle')}</h5>
                <div className="d-flex flex-wrap gap-2">
                    {validCoins.map(coin => (
                        <button
                            key={coin}
                            className="btn btn-outline-success rounded-pill px-3 py-2"
                            onClick={() => onInsertCoin(coin)}
                            aria-label={t('insertCoinButtonLabel', { coinValue: coin.toFixed(2) })}
                        >
                            {coin.toFixed(2)}
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );
};
export default CoinInserter;