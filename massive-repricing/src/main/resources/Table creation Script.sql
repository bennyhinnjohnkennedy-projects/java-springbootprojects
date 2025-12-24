
-- Create repricing_job table
CREATE TABLE keplero.repricing_job (
    id serial4 PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL,
    input_file_name VARCHAR(512) NOT NULL UNIQUE,
    job_status VARCHAR(50) NOT NULL,
    start_time TIMESTAMP without TIME ZONE,
    end_time TIMESTAMP without TIME ZONE,
    total_rows INT DEFAULT 0,
    processed INT DEFAULT 0,
    succeeded INT DEFAULT 0,
    failed INT DEFAULT 0
);

-- Create repricing_job_details table
CREATE TABLE keplero.repricing_job_details (
    id serial4 PRIMARY KEY,
    repricing_job_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    contract_id VARCHAR(255) NOT NULL,
    process_name VARCHAR(50) NOT NULL,
    status VARCHAR(50),
    start_time TIMESTAMP without TIME ZONE,
    end_time TIMESTAMP without TIME ZONE,
    stack_trace TEXT,
    CONSTRAINT fk_repricing_job FOREIGN KEY (repricing_job_id) REFERENCES repricing_job (id)
);

-- Create repricing_config table
CREATE TABLE keplero.repricing_config (
	id serial4 PRIMARY KEY,
	config_name varchar(255) NOT NULL,
	config_value varchar NOT NULL,
	active bool DEFAULT true NOT NULL,
	description varchar NULL,
	created_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	updated_time timestamp DEFAULT CURRENT_TIMESTAMP NULL
);

-- Create indexes for performance optimization
CREATE INDEX idx_repricing_job_status ON repricing_job_details (file_name);
