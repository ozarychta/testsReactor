package edu.iis.mto.testreactor.exc3;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

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
    public void itCompiles(){
        atmMachine.withdraw(money, card);
    }



}
