import React from 'react';

interface CoinInserterProps {
    onInsertCoin: (value: number) => void;
}

const CoinInserter: React.FC<CoinInserterProps> = ({ onInsertCoin }) => {
    const validCoins = [0.5, 1.0, 2.0, 5.0, 10.0];

    return (
        <div className="card shadow-sm rounded-3 mb-4"> {/* Bootstrap shadow and border-radius */}
            <div className="card-body">
                <h5 className="card-title mb-3 text-primary">Insert Coins (MAD)</h5>
                <div className="d-flex flex-wrap gap-2"> {/* For responsive wrapping of buttons */}
                    {validCoins.map(coin => (
                        <button
                            key={coin}
                            className="btn btn-outline-success rounded-pill px-3 py-2" /* Pill shape, padding */
                            onClick={() => onInsertCoin(coin)}
                            aria-label={`Insert ${coin.toFixed(2)} MAD`}
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