package edu.iis.mto.testreactor.exc3;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AtmMachineTest {

    private AtmMachine atmMachine;
    private Money money;
    private Card card;
    private AuthenticationToken token;

    @Mock
    private CardProviderService cardService;
    @Mock
    private BankService bankService;
    @Mock
    private MoneyDepot moneyDepot;

    @Before
    public void setUp() throws CardAuthorizationException, InsufficientFundsException, MoneyDepotException{
        atmMachine = new AtmMachine(cardService,bankService, moneyDepot);
        money = Money.builder().withAmount(100).withCurrency(Currency.PL).build();
        card = Card.builder().withCardNumber("1111").withPinNumber(1111).build();
        token = AuthenticationToken.builder().withAuthorizationCode(1).withUserId("11").build();
        when(cardService.authorize(card)).thenReturn(token);
        doNothing().when(bankService).startTransaction(token);
        doNothing().when(bankService).charge(token, money);
        doNothing().when(bankService).commit(token);
        doNothing().when(bankService).abort(token);
        doNothing().when(moneyDepot).releaseBanknotes(anyListOf(Banknote.class));
    }

    @Test
    public void withdraw_amountIs100PL_shouldReleaseOneBanknote100PL(){
        money = Money.builder().withAmount(100).withCurrency(Currency.PL).build();
        Payment actual = atmMachine.withdraw(money, card);

        assertThat(actual.getValue().size(),is(1));
        assertThat(actual.getValue().get(0).getValue(), is(100));
        assertThat(actual.getValue().get(0).getCurrency(), is(Currency.PL));
    }

    @Test
    public void withdraw_amountIs120PL_shouldReleaseTwoBanknotes100And20PL(){
        money = Money.builder().withAmount(120).withCurrency(Currency.PL).build();
        Payment actual = atmMachine.withdraw(money, card);

        assertThat(actual.getValue().size(),is(2));
        assertThat(actual.getValue().get(0).getValue(), is(20));
        assertThat(actual.getValue().get(0).getCurrency(), is(Currency.PL));
        assertThat(actual.getValue().get(1).getValue(), is(100));
        assertThat(actual.getValue().get(1).getCurrency(), is(Currency.PL));
    }

    @Test(expected = WrongMoneyAmountException.class)
    public void withdraw_amountToSmallToPayInBanknotes_shouldThrowException(){
        money = Money.builder().withAmount(2).withCurrency(Currency.PL).build();
        atmMachine.withdraw(money, card);
    }

    @Test(expected = AtmException.class)
    public void withdraw_moneyDepotDidNotReleaseBanknotes_shouldThrowException() throws MoneyDepotException{
        doThrow(MoneyDepotException.class).when(moneyDepot).releaseBanknotes(anyListOf(Banknote.class));
        atmMachine.withdraw(money, card);
    }

    @Test
    public void withdraw_moneyDepotReleasedBanknotes_shouldInvokeCommitMethod() throws MoneyDepotException{
        doNothing().when(moneyDepot).releaseBanknotes(anyListOf(Banknote.class));
        atmMachine.withdraw(money, card);
        verify(bankService).commit(token);
    }

    @Test(expected = AtmException.class)
    public void withdraw_authorizationError_shouldThrowException() throws CardAuthorizationException{
        when(cardService.authorize(card)).thenThrow(CardAuthorizationException.class);
        atmMachine.withdraw(money, card);
    }

}
