/**
 * DecomposedDouble: decomposition of a double into parts
 *
 * Note: there seems to have been a better way of dealing with this than this.
 * A better method would have been using "Digits * 10^pow" instead and re-defining operations
 * But this still works
 */

package dhkim16.example.com.simplecalculator;

public class DecomposedDouble
{
    public long int_part;
    public boolean is_negative;
    public boolean decimal;
    public String decimal_part;

    public void clear()
    {
        int_part = 0;
        is_negative = false;
        decimal = false;
        decimal_part = "";
    }

    public DecomposedDouble()
    {
        int_part = 0;
        is_negative = false;
        decimal = false;
        decimal_part = "";
    }

    @Override
    public String toString()
    {
        String result_str = "";
        if (int_part == 0 && is_negative)
        {
            result_str += "-";
        }
        result_str += String.valueOf(int_part);
        if (decimal && decimal_part.length() > 0)
        {
            result_str += "." + decimal_part;
        }
        return result_str;
    }

    public double toDouble()
    {
        return Double.valueOf(toString());
    }

    public boolean fromDouble(double num, int num_digits)
    {
        is_negative = (num < 0.0);

        int pow;
        for (pow = 0; pow < num_digits - 1; pow++)
        {
            if (num > Math.pow(10.0, num_digits - 1))
            {
                break;
            }
            num *= 10;
        }
        long rounded_num = (long) num;
        if (String.valueOf(rounded_num).length() > num_digits)
        {
            rounded_num /= 10;
            pow--;
        }
        if (pow < 0)
        {
            return false;
        }
        long mod_num = (long) Math.pow(10.0, pow);
        int_part = rounded_num / mod_num;
        long remainder = rounded_num % mod_num;
        if (remainder == 0)
        {
            decimal = false;
            decimal_part = "";
        }
        else
        {
            decimal = true;
            decimal_part = "";
            for (int i = 0; i < pow; i++)
            {
                if (remainder != 0)
                {
                    long digit = remainder % 10;
                    if (digit != 0 || decimal_part.length() != 0) {
                        decimal_part = String.valueOf(digit) + decimal_part;
                    }
                    remainder /= 10;
                }
                else
                {
                    decimal_part = "0" + decimal_part;
                }
            }
        }
        return true;
    }
}
