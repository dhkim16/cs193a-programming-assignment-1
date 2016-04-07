/**
 * Dae Hyun Kim (dhkim16@stanford.edu)
 * SimpleCalculator v1.00
 *
 * This app is a simple calculator app that simulates a normal commercial calculator.
 * The app performs basic operations, and percent operations.
 * In addition, the app provides memory functionality (M+, M-, MR, MC).
 * Up to 12 digits can be used for computation:
 * Numbers of greater absolute values than 10e12 are considered overflows.
 * The smallest unit that can be used is 10e-11.
 * This app is designed to be essentially fool-proof -- it should handle most misuses.
 *
 * Note: Designed for Nexus 5. Devices with smaller screens may have numbers spanning through
 * multiple lines. Devices with larger screens should have no issues.
 */

package dhkim16.example.com.simplecalculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CalculatorActivity extends AppCompatActivity
{
    private DecomposedDouble curr_num;
    private Operation curr_op = Operation.NO_OP;
    private double prev_num;
    private double mem_num;
    NumericalError error_state = NumericalError.NO_ERROR;
    private boolean prev_is_op = false;
    private boolean prev_is_eq = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        curr_num = new DecomposedDouble();
        refresh_display();
    }

    private void refresh_display()
    {
        TextView result_tv = (TextView) findViewById(R.id.result_tv);
        result_tv.setText("");
        switch (error_state)
        {
            case NO_ERROR:
            {
                result_tv.setText(curr_num.toString());
                break;
            }
            case NEGATIVE_OVERFLOW:
            {
                result_tv.setText("-");
            }
            case POSITIVE_OVERFLOW:
            {
                result_tv.append("INFTY");
                break;
            }
            case DIV_BY_ZERO:
            {
                result_tv.setText("DIVISION BY 0");
                break;
            }
        }
    }

    private double perform_op()
    {
        double result = 0.0;
        switch (curr_op)
        {
            case NO_OP:
            {
                result = curr_num.toDouble();
                break;
            }
            case PLUS:
            {
                result = prev_num + curr_num.toDouble();
                break;
            }
            case MINUS:
            {
                result = prev_num - curr_num.toDouble();
                break;
            }
            case TIMES:
            {
                result = prev_num * curr_num.toDouble();
                break;
            }
            case DIV:
            {
                double curr_num_d = curr_num.toDouble();
                if (curr_num_d == 0.0)
                {
                    error_state = NumericalError.DIV_BY_ZERO;
                    result = Double.NaN;
                }
                else
                {
                    result = prev_num / curr_num_d;
                }
                break;
            }
        }
        if (result >= 1e12)
        {
            error_state = NumericalError.POSITIVE_OVERFLOW;
        }
        else if (result <= -1e12)
        {
            error_state = NumericalError.NEGATIVE_OVERFLOW;
        }
        return result;
    }

    public void digit_button_click(View view)
    {
        if (prev_is_op || prev_is_eq)
        {
            curr_num.clear();
            prev_is_op = false;
            prev_is_eq = false;
        }
        String new_digits = ((Button) view).getText().toString();
        if (curr_num.decimal)
        {
            int curr_len = curr_num.decimal_part.length()
                    + String.valueOf(curr_num.int_part).length() - (curr_num.is_negative ? 1 : 0);
            if (curr_len < 12)
            {
                if (curr_len == 11 && new_digits.length() == 2)
                {
                    new_digits = "0";
                }
                curr_num.decimal_part += new_digits;
            }
        }
        else
        {
            if (new_digits.length() == 1)
            {
                curr_num.int_part =
                        curr_num.int_part * 10
                                + (curr_num.is_negative ? -1 : 1) * Long.valueOf(new_digits);
            }
            else
            {
                curr_num.int_part *= 100;
            }
            if (curr_num.int_part >= 1e12)
            {
                error_state = NumericalError.POSITIVE_OVERFLOW;
            }
            else if (curr_num.int_part <= -1e12)
            {
                error_state = NumericalError.NEGATIVE_OVERFLOW;
            }
        }
        refresh_display();
    }

    private void clear()
    {
        curr_num.clear();
        curr_op = Operation.NO_OP;
        prev_num = 0.0;
        error_state = NumericalError.NO_ERROR;
        prev_is_op = false;
        prev_is_eq = false;
    }

    public void clear_button_click(View view)
    {
        clear();
        refresh_display();
    }

    public void decimal_button_click(View view)
    {
        if (prev_is_op || prev_is_eq)
        {
            curr_num.clear();
            prev_is_op = false;
            prev_is_eq = false;
        }
        curr_num.decimal = true;
        refresh_display();
    }

    public void sign_button_click(View view)
    {
        prev_is_op = false;
        prev_is_eq = false;
        curr_num.int_part *= -1;
        curr_num.is_negative = !curr_num.is_negative;
        refresh_display();
    }

    public void operation_button_click(View view)
    {
        if (!prev_is_op)
        {
            prev_num = perform_op();
            if (!curr_num.fromDouble(prev_num, 12))
            {
                error_state =
                        (curr_num.is_negative ?
                                NumericalError.NEGATIVE_OVERFLOW :
                                NumericalError.POSITIVE_OVERFLOW);
            }
        }

        String selected_op = ((Button) view).getText().toString();
        switch (selected_op) {
            case "+":
                curr_op = Operation.PLUS;
                break;
            case "-":
                curr_op = Operation.MINUS;
                break;
            case "x":
                curr_op = Operation.TIMES;
                break;
            case "/":
                curr_op = Operation.DIV;
                break;
        }

        prev_is_op = true;
        prev_is_eq = false;
        refresh_display();
    }

    public void equal_button_click(View view)
    {
        if (!curr_num.fromDouble(perform_op(), 12))
        {
            error_state =
                    (curr_num.is_negative ?
                            NumericalError.NEGATIVE_OVERFLOW : NumericalError.POSITIVE_OVERFLOW);
        }
        prev_is_op = false;
        prev_is_eq = true;
        curr_op = Operation.NO_OP;
        refresh_display();
    }

    public void mem_operation_click(View view)
    {
        if (error_state != NumericalError.NO_ERROR)
        {
            return;
        }

        switch (view.getId())
        {
            case R.id.mplus_button:
                mem_num += curr_num.toDouble();
                break;
            case R.id.mminus_button:
                mem_num -= curr_num.toDouble();
                break;
        }
    }

    public void mem_recall_click(View view)
    {
        if (!curr_num.fromDouble(mem_num, 12))
        {
            error_state =
                    (curr_num.is_negative ?
                            NumericalError.NEGATIVE_OVERFLOW : NumericalError.POSITIVE_OVERFLOW);
        }
        prev_is_op = false;
        prev_is_eq = true;
        refresh_display();
    }

    public void mem_clear_button_click(View view)
    {
        mem_num = 0.0;
    }

    public void all_clear_button_click(View view)
    {
        clear();
        mem_num = 0;
        refresh_display();
    }

    public void percent_button_click(View view)
    {
        if (curr_op == Operation.NO_OP)
        {
            return;
        }
        curr_num.fromDouble(prev_num * curr_num.toDouble() / 100.0, 14);
        if (!curr_num.fromDouble(perform_op(), 12))
        {
            error_state =
                    (curr_num.is_negative ?
                            NumericalError.NEGATIVE_OVERFLOW : NumericalError.POSITIVE_OVERFLOW);
        }
        prev_is_op = false;
        prev_is_eq = true;
        curr_op = Operation.NO_OP;
        refresh_display();
    }

    public void back_button_click(View view)
    {
        prev_is_op = false;
        prev_is_eq = false;
        if (curr_num.decimal_part.length() > 0)
        {
            curr_num.decimal_part =
                    curr_num.decimal_part.substring(0, curr_num.decimal_part.length() - 1);
        }
        else if (curr_num.decimal)
        {
            curr_num.decimal = false;
        }
        else
        {
            curr_num.int_part /= 10;
            if (curr_num.int_part == 0)
            {
                curr_num.is_negative = false;
            }
        }
        refresh_display();
    }
}
