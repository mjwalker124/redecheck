package shef.utils;

import org.apache.commons.lang3.time.StopWatch;

/** Created by thomaswalsh on 22/02/2016. */
public class StopwatchFactory {
  private StopWatch total;
  private StopWatch setup;
  private StopWatch capture;
  private StopWatch sleep;
  private StopWatch extract;
  private StopWatch rlg;
  private StopWatch detect;
  private StopWatch report;
  private StopWatch process;

  public StopwatchFactory() {
    total = new StopWatch();
    setup = new StopWatch();
    capture = new StopWatch();
    sleep = new StopWatch();
    extract = new StopWatch();
    process = new StopWatch();
    rlg = new StopWatch();
    detect = new StopWatch();
    report = new StopWatch();
  }

  public StopWatch getRlg() {
    return rlg;
  }

  public StopWatch getProcess() {
    return process;
  }

  public StopWatch getTotal() {
    return total;
  }

  public StopWatch getCapture() {
    return capture;
  }

  public StopWatch getSleep() {
    return sleep;
  }

  public StopWatch getExtract() {
    return extract;
  }

  public StopWatch getSetup() {
    return setup;
  }

  public StopWatch getDetect() {
    return detect;
  }

  public StopWatch getReport() {
    return report;
  }
}
