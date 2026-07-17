CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    username VARCHAR(255),
    action VARCHAR(50) NOT NULL,
    details TEXT,
    endpoint VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    ip_address VARCHAR(45),
    success BOOLEAN NOT NULL DEFAULT true,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
