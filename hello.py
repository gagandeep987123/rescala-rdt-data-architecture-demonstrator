import subprocess, os, psutil


# pid = os.fork()
# if pid == 0:
#    subprocess.run(["Python3","tensecwait.py"])


def is_program_running(program_name):
    """
    Check if a program with the given name is running.
    """
    for proc in psutil.process_iter(['pid', 'name', 'cmdline']):
        try:
            if proc.info['cmdline'] is not None and program_name in proc.info['cmdline']:
                return True
        except (psutil.AccessDenied, psutil.NoSuchProcess):
            pass
    return False


if is_program_running("tensecwait.py"):
    print("Program is running")
else:
    print("starting a process")
    pid = os.fork()
    if pid == 0:
        subprocess.run(["python3", "tensecwait.py"])
    
