\set ON_ERROR_STOP

SET SESSION AUTHORIZATION 'tms';

-- Add modem enabled boolean column
ALTER TABLE iris.modem ADD COLUMN enabled BOOLEAN;
UPDATE iris.modem SET enabled = TRUE;
ALTER TABLE iris.modem ALTER COLUMN enabled SET NOT NULL;

-- Add enabled column to modem_view
CREATE OR REPLACE VIEW modem_view AS
	SELECT name, uri, config, timeout, enabled
	FROM iris.modem;
GRANT SELECT ON modem_view TO PUBLIC;