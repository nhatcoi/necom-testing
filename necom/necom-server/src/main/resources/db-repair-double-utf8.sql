-- One-time repair: UTF-8 text was interpreted as latin1 then stored again (double encoding).
-- Typical symptom: names look like "HÃ  Ná»™i" when viewed as UTF-8, or wrong Vietnamese in app.
-- Only rows where the conversion succeeds are updated (others are left unchanged).
-- Do NOT run if names are already correct (it would corrupt good data).
SET NAMES utf8mb4;

UPDATE province AS p
         INNER JOIN (SELECT id,
                            CONVERT(CAST(CONVERT(name USING latin1) AS BINARY) USING utf8mb4) AS fixed
                     FROM province) AS t ON p.id = t.id
SET p.name = t.fixed
WHERE t.fixed IS NOT NULL;

UPDATE ward AS w
         INNER JOIN (SELECT id,
                            CONVERT(CAST(CONVERT(name USING latin1) AS BINARY) USING utf8mb4) AS fixed
                     FROM ward) AS t ON w.id = t.id
SET w.name = t.fixed
WHERE t.fixed IS NOT NULL;
