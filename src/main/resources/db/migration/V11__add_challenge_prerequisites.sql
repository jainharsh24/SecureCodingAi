CREATE TABLE challenge_prerequisites (
  challenge_id BIGINT NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
  prerequisite_challenge_id BIGINT NOT NULL REFERENCES challenges(id) ON DELETE CASCADE,
  PRIMARY KEY (challenge_id, prerequisite_challenge_id),
  CONSTRAINT chk_challenge_prerequisites_not_self CHECK (challenge_id <> prerequisite_challenge_id)
);
CREATE INDEX idx_challenge_prerequisites_prerequisite ON challenge_prerequisites(prerequisite_challenge_id);

INSERT INTO challenge_prerequisites(challenge_id,prerequisite_challenge_id)
SELECT target.id,required.id FROM (VALUES
('SQL Injection','SQL Query Concatenation'),('SQL Injection','Prepared Statement Lookup'),
('NoSQL Injection','NoSQL Document Values'),('NoSQL Injection','NoSQL Equality Criteria'),
('Command Injection','Approved Report Launcher'),('Command Injection','ProcessBuilder Fixed Arguments'),
('LDAP Injection','Directory Search Filter'),('LDAP Injection','LDAP Special-Character Escaping'),
('XPath Injection','XPath Values and Expressions'),('XPath Injection','XPath Variable Binding'),
('XML Injection','XML Text Nodes'),('CRLF Injection','Response Header Value Check'),
('Template/SSTI Injection','Welcome Template Data'),('Template/SSTI Injection','Template Output Context'),
('Code Injection','Code and Data Separation'),('Code Injection','Data-Driven Rule Selection'),
('Email Header Injection','Email Subject Header Check'),('XXE Injection','XML Parser Entity Protection'),('XXE Injection','Secure XML Parser Configuration'),
('GraphQL Injection','GraphQL Variables and ID Check'),('GraphQL Injection','GraphQL Query Variables'),('GraphQL Injection','Typed GraphQL Variables'),
('SQL Search with Two Parameters','SQL Query Concatenation'),('SQL Search with Two Parameters','Prepared Statement Lookup'),
('NoSQL Equality Criteria','NoSQL Document Values'),('ProcessBuilder Fixed Arguments','Approved Report Launcher'),('Canonical Report Path','Report Directory Resolver'),('LDAP Special-Character Escaping','Directory Search Filter'),('XPath Variable Binding','XPath Values and Expressions'),('GraphQL Query Variables','GraphQL Variables and ID Check'),
('SQL Sort-Field Allowlist','SQL Search with Two Parameters'),('NoSQL Operator Injection','NoSQL Equality Criteria'),('Approved Command Modes','ProcessBuilder Fixed Arguments'),('Secure XML Parser Configuration','XML Parser Entity Protection'),('Template Output Context','Welcome Template Data'),('Template Output Context','HTML Text Encoder'),('Data-Driven Rule Selection','Code and Data Separation'),('Typed GraphQL Variables','GraphQL Query Variables'),
('Secure SQL Report Builder','SQL Search with Two Parameters'),('Secure SQL Report Builder','SQL Sort-Field Allowlist'),('Secure XML Import Pipeline','Secure XML Parser Configuration'),('Secure Notification Composer','Template Output Context'),('Secure Notification Composer','Response Header Value Check'),('Secure Notification Composer','Email Subject Header Check')
) edge(target_title,required_title)
JOIN challenges target ON target.title=edge.target_title AND target.is_active=TRUE
JOIN challenges required ON required.title=edge.required_title AND required.is_active=TRUE;
