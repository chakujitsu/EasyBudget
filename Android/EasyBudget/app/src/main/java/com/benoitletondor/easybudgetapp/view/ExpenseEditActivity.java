package com.benoitletondor.easybudgetapp.view;

import android.animation.Animator;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.benoitletondor.easybudgetapp.R;
import com.benoitletondor.easybudgetapp.helper.UIHelper;
import com.benoitletondor.easybudgetapp.helper.CurrencyHelper;
import com.benoitletondor.easybudgetapp.model.Expense;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Activity to add a new expense
 *
 * @author Benoit LETONDOR
 */
public class ExpenseEditActivity extends DBActivity
{
    /**
     * Save floating action button
     */
    private FloatingActionButton fab;
    /**
     * Edit text that contains the description
     */
    private EditText             descriptionEditText;
    /**
     * Edit text that contains the amount
     */
    private EditText             amountEditText;
    /**
     * Button for date selection
     */
    private Button               dateButton;
    /**
     * Textview that displays the type of expense
     */
    private TextView             expenseType;
    /**
     * Expense that is being edited (will be null if it's a new one)
     */
    private Expense              expense;
    /**
     * The date of the expense
     */
    private Date                 date;
    /**
     * Is the new expense a revenue
     */
    private boolean isRevenue = false;

// -------------------------------------->

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_edit);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        date = (Date) getIntent().getSerializableExtra("date");

        if (getIntent().hasExtra("expense"))
        {
            expense = (Expense) getIntent().getSerializableExtra("expense");
            isRevenue = expense.getAmount() < 0;
            date = expense.getDate();

            setTitle(R.string.title_activity_edit_expense);
        }

        setUpButtons();
        setUpTextFields();
        setUpDateButton();

        setResult(RESULT_CANCELED);

        if ( UIHelper.willAnimateActivityEnter(this) )
        {
            UIHelper.animateActivityEnter(this, new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart(Animator animation)
                {

                }

                @Override
                public void onAnimationEnd(Animator animation)
                {
                    UIHelper.setFocus(descriptionEditText);
                    UIHelper.animateFABAppear(fab);
                }

                @Override
                public void onAnimationCancel(Animator animation)
                {

                }

                @Override
                public void onAnimationRepeat(Animator animation)
                {

                }
            });
        }
        else
        {
            UIHelper.setFocus(descriptionEditText);
            UIHelper.animateFABAppear(fab);
        }
    }

// ----------------------------------->

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if( id == android.R.id.home ) // Back button of the actionbar
        {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

// ----------------------------------->

    /**
     * Validate user inputs
     *
     * @return true if user inputs are ok, false otherwise
     */
    private boolean validateInputs()
    {
        boolean ok = true;

        String description = descriptionEditText.getText().toString();
        if( description.trim().isEmpty() )
        {
            descriptionEditText.setError("Enter a description"); //TODO translate
            ok = false;
        }

        String amount = amountEditText.getText().toString();
        if( amount.trim().isEmpty() )
        {
            amountEditText.setError("Enter an amount"); //TODO translate
            ok = false;
        }
        else
        {
            try
            {
                int value = Integer.parseInt(amount);
                if( value <= 0 )
                {
                    amountEditText.setError("Amount should be greater than 0"); //TODO
                    ok = false;
                }
            }
            catch(Exception e)
            {
                amountEditText.setError("Not a valid amount"); //TODO
                ok = false;
            }
        }

        return ok;
    }

    /**
     * Set-up revenue and payment buttons
     */
    private void setUpButtons()
    {
        expenseType = (TextView) findViewById(R.id.expense_type_tv);

        SwitchCompat expenseTypeSwitch = (SwitchCompat) findViewById(R.id.expense_type_switch);
        expenseTypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                isRevenue = isChecked;
                setExpenseTypeTextViewLayout();
            }
        });

        // Init value to checked if already a revenue (can be true if we are editing an expense)
        if( isRevenue )
        {
            expenseTypeSwitch.setChecked(true);
            setExpenseTypeTextViewLayout();
        }

        fab = (FloatingActionButton) findViewById(R.id.save_expense_fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (validateInputs())
                {
                    int value = Integer.parseInt(amountEditText.getText().toString());

                    Expense expenseToSave;
                    if (expense == null)
                    {
                        expenseToSave = new Expense(descriptionEditText.getText().toString(), isRevenue ? -value : value, date);
                    }
                    else
                    {
                        expenseToSave = expense;
                        expenseToSave.setTitle(descriptionEditText.getText().toString());
                        expenseToSave.setAmount(isRevenue ? -value : value);
                        expenseToSave.setDate(date);
                    }

                    db.persistExpense(expenseToSave);

                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }

    /**
     * Set revenue text view layout
     */
    private void setExpenseTypeTextViewLayout()
    {
        if( isRevenue )
        {
            expenseType.setText(R.string.income);
            expenseType.setTextColor(ContextCompat.getColor(this, R.color.budget_green));
        }
        else
        {
            expenseType.setText(R.string.payment);
            expenseType.setTextColor(ContextCompat.getColor(this, R.color.budget_red));
        }
    }

    /**
     * Set up text field focus behavior
     */
    private void setUpTextFields()
    {
        ((TextInputLayout) findViewById(R.id.amount_inputlayout)).setHint(String.format(Locale.US, getResources().getString(R.string.amount), CurrencyHelper.getUserCurrency(this).getSymbol()));

        descriptionEditText = (EditText) findViewById(R.id.description_edittext);

        if( expense != null )
        {
            descriptionEditText.setText(expense.getTitle());
            descriptionEditText.setSelection(descriptionEditText.getText().length()); // Put focus at the end of the text
        }

        amountEditText = (EditText) findViewById(R.id.amount_edittext);

        if( expense != null )
        {
            amountEditText.setText(String.valueOf(Math.abs(expense.getAmount())));
        }
    }

    /**
     * Set up the date button
     */
    private void setUpDateButton()
    {
        dateButton = (Button) findViewById(R.id.date_button);
        UIHelper.removeButtonBorder(dateButton); // Remove border on lollipop

        updateDateButtonDisplay();

        dateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatePickerDialogFragment fragment = new DatePickerDialogFragment(date, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                    {
                        Calendar cal = Calendar.getInstance();

                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, monthOfYear);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        date = cal.getTime();
                        updateDateButtonDisplay();
                    }
                });

                fragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }

    private void updateDateButtonDisplay()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy", Locale.US);
        dateButton.setText(formatter.format(date));
    }
}