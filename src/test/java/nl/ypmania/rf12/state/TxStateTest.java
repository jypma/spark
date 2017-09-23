package nl.ypmania.rf12.state;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.protobuf.ByteString;

import nl.ypmania.env.Environment;
import nl.ypmania.env.Zone;
import nl.ypmania.rf12.RF12Packet;
import nl.ypmania.rf12.RF12Service;

public class TxStateTest {
  @Mock Environment env;
  @Mock Zone zone;
  @Mock RF12Service rf12Service;
  @Mock Timer timer;
  
  TxState txState;
  
  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    when(env.getRf12Service()).thenReturn(rf12Service);
    when(env.getTimer()).thenReturn(timer);
    
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        invocation.getArgumentAt(0, Runnable.class).run();
        return null;
      }
    }).when(env).onRf868Clear(any(Runnable.class));
    
    txState = new TxState(env, zone, 15, ByteString.copyFromUtf8("initial"));
  }
  
  @Test
  public void should_not_send_packets_when_setting_same_state() {
    txState.setState(ByteString.copyFromUtf8("initial"));
    
    verifyNoMoreInteractions(rf12Service);
  }
  
  @Test
  public void should_send_packet_when_changing_state() {
    txState.setState(ByteString.copyFromUtf8("hello"));
    
    ArgumentCaptor<RF12Packet> packet = ArgumentCaptor.forClass(RF12Packet.class);
    verify(rf12Service).queue(eq(zone), packet.capture());
    assertEquals(2, packet.getValue().getHeader());
    assertEquals(Arrays.asList(
        1 << 3, 15,    // nodeId = 15 
        2 << 3, 1,     // seq = 1 
        3 << 3 | 2, 5, // body, length 5
          104, 101, 108, 108, 111  // "hello"
    ), packet.getValue().getContents());
  }
  
  @Test
  public void should_resend_if_no_ack_within_timeout() {
    txState.setState(ByteString.copyFromUtf8("hello"));
    
    ArgumentCaptor<TimerTask> task = ArgumentCaptor.forClass(TimerTask.class);
    verify(timer).schedule(task.capture(), eq(160l));
    
    reset(rf12Service);
    task.getValue().run();
    
    ArgumentCaptor<RF12Packet> packet = ArgumentCaptor.forClass(RF12Packet.class);
    verify(rf12Service).queue(eq(zone), packet.capture());
    assertEquals(2, packet.getValue().getHeader());
    assertEquals(Arrays.asList(
        1 << 3, 15,    // nodeId = 15 
        2 << 3, 1,     // seq = 1 
        3 << 3 | 2, 5, // body, length 5
          104, 101, 108, 108, 111  // "hello"
    ), packet.getValue().getContents());
  }
  
  @Test
  public void should_not_resend_if_received_ack() {
    txState.setState(ByteString.copyFromUtf8("hello"));
    
    ArgumentCaptor<TimerTask> task = ArgumentCaptor.forClass(TimerTask.class);
    verify(timer).schedule(task.capture(), eq(160l));

    RF12Packet ack = new RF12Packet(1, new int[] {
        1 << 3, 15,    // nodeId = 15 
        2 << 3, 1      // seq = 1         
    });
    txState.receive(ack);
    
    reset(rf12Service);
    task.getValue().run();
    
    verifyNoMoreInteractions(rf12Service);
  }
  
  @Test
  public void should_resend_if_received_ack_for_different_node() {
    txState.setState(ByteString.copyFromUtf8("hello"));
    
    ArgumentCaptor<TimerTask> task = ArgumentCaptor.forClass(TimerTask.class);
    verify(timer).schedule(task.capture(), eq(160l));

    RF12Packet ack = new RF12Packet(1, new int[] {
        1 << 3, 42,    // nodeId = 42, wrong 
        2 << 3, 1      // seq = 1         
    });
    txState.receive(ack);
    
    reset(rf12Service);
    task.getValue().run();
    
    ArgumentCaptor<RF12Packet> packet = ArgumentCaptor.forClass(RF12Packet.class);
    verify(rf12Service).queue(eq(zone), packet.capture());
    assertEquals(2, packet.getValue().getHeader());
    assertEquals(Arrays.asList(
        1 << 3, 15,    // nodeId = 15 
        2 << 3, 1,     // seq = 1 
        3 << 3 | 2, 5, // body, length 5
          104, 101, 108, 108, 111  // "hello"
    ), packet.getValue().getContents());    
  }

  @Test
  public void should_resend_if_received_ack_for_different_seq() {
    txState.setState(ByteString.copyFromUtf8("hello"));
    
    ArgumentCaptor<TimerTask> task = ArgumentCaptor.forClass(TimerTask.class);
    verify(timer).schedule(task.capture(), eq(160l));

    RF12Packet ack = new RF12Packet(1, new int[] {
        1 << 3, 15,    // nodeId = 15 
        2 << 3, 2      // seq = 2, wrong        
    });
    txState.receive(ack);
    
    reset(rf12Service);
    task.getValue().run();
    
    ArgumentCaptor<RF12Packet> packet = ArgumentCaptor.forClass(RF12Packet.class);
    verify(rf12Service).queue(eq(zone), packet.capture());
    assertEquals(2, packet.getValue().getHeader());
    assertEquals(Arrays.asList(
        1 << 3, 15,    // nodeId = 15 
        2 << 3, 1,     // seq = 1 
        3 << 3 | 2, 5, // body, length 5
          104, 101, 108, 108, 111  // "hello"
    ), packet.getValue().getContents());    
  }
}
