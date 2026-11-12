CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    target_user_id UUID,
    type VARCHAR(20) NOT NULL
        CHECK (type IN ('DEPOSITO', 'RETIRADA', 'TRANSFERENCIA', 'COMPRA')),
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('APROVADA', 'REJEITADA')),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    usd_rate NUMERIC(19, 8),
    eur_rate NUMERIC(19, 8),
    rate_date DATE,
    description VARCHAR(255),
    category VARCHAR(50)
        CHECK (category IS NULL OR category IN (
            'ALIMENTACAO', 'TRANSPORTE', 'MORADIA', 'SAUDE', 'LAZER', 'EDUCACAO', 'OUTROS'
        )),
    rejection_reason VARCHAR(255),
    original_transaction_id UUID REFERENCES transactions(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_transaction_rejection_reason CHECK (
        (status = 'APROVADA' AND rejection_reason IS NULL)
        OR (status = 'REJEITADA' AND rejection_reason IS NOT NULL
            AND length(trim(rejection_reason)) > 0)
    ),
    CONSTRAINT chk_rate_group CHECK (
        (usd_rate IS NULL AND eur_rate IS NULL AND rate_date IS NULL)
        OR (usd_rate IS NOT NULL AND usd_rate > 0
            AND eur_rate IS NOT NULL AND eur_rate > 0
            AND rate_date IS NOT NULL)
    ),
    CONSTRAINT chk_approved_has_rate CHECK (
        status <> 'APROVADA'
        OR (usd_rate IS NOT NULL AND eur_rate IS NOT NULL AND rate_date IS NOT NULL)
    ),
    CONSTRAINT chk_transfer_destination CHECK (
        type <> 'TRANSFERENCIA' OR target_user_id IS NOT NULL
    ),
    CONSTRAINT chk_purchase_category CHECK (
        type <> 'COMPRA' OR category IS NOT NULL
    )
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_user_status ON transactions(user_id, status);
