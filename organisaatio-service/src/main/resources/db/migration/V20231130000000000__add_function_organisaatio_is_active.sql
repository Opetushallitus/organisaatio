CREATE FUNCTION organisaatio_is_active(o organisaatio)
    RETURNS boolean
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN o.alkupvm <= current_date AND(o.lakkautuspvm IS NULL OR current_date < o.lakkautuspvm) AND NOT o.organisaatiopoistettu;
END;
$$;
